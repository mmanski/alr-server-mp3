package com.rasgrass.alr.server.mp3;

import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

@Path("listen")
@Singleton
public class MediaResource {

	final int chunk_size = 1024 * 1024; // 1MB 
	private File audio;

	@HEAD
	@Produces("audio/mp3")
	public Response header() {
		return Response.ok().status(206).header(HttpHeaders.CONTENT_LENGTH, audio.length()).build();
	}

	@GET
	@Produces("audio/mp3")
	public Response streamAudio(@QueryParam("file") String filename, @HeaderParam("Range") String range) throws Exception {
		URL url = this.getClass().getResource("/" + filename);
		audio = new File(url.getFile());
		return buildStream(audio, range);
	}

	private Response buildStream(final File asset, final String range) throws Exception {
		// range not requested : Firefox, Opera, IE do not send range headers
		if (range == null) {
			StreamingOutput streamer = new StreamingOutput() {
				@Override
				public void write(final OutputStream output) throws IOException, WebApplicationException {

					FileChannel inputChannel = new FileInputStream(asset).getChannel();
					WritableByteChannel outputChannel = Channels.newChannel(output);
					try {
						inputChannel.transferTo(0, inputChannel.size(), outputChannel);
					} finally {
						inputChannel.close();
						outputChannel.close();
					}
				}
			};
			return Response.ok(streamer).status(200).header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
		}

		String[] ranges = range.split("=")[1].split("-");
		int from = Integer.parseInt(ranges[0]);

		int to = chunk_size + from;
		if (to >= asset.length()) {
			to = (int) (asset.length() - 1);
		}
		if (ranges.length == 2) {
			to = Integer.parseInt(ranges[1]);
		}

		String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());
		RandomAccessFile raf = new RandomAccessFile(asset, "r");
		raf.seek(from);

		int len = to - from + 1;
		MediaStreamer streamer = new MediaStreamer(len, raf);
		Response.ResponseBuilder res = Response.ok(streamer).status(206)
				.header("Accept-Ranges", "bytes")
				.header("Content-Range", responseRange)
				.header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
				.header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));
		return res.build();
	}

}

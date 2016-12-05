package de.rainu.restcommander.process;

import org.apache.commons.exec.ExecuteStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeStreamHandler implements ExecuteStreamHandler {
	private List<ExecuteStreamHandler> handler = new ArrayList<>();

	public CompositeStreamHandler(ExecuteStreamHandler...handler) {
		addHandler(handler);
	}

	public void addHandler(ExecuteStreamHandler...handler){
		this.handler.addAll(Arrays.asList(handler));
	}

	@Override
	public void setProcessInputStream(OutputStream os) throws IOException {
		handler.stream().forEach(handler -> {
			try {
				handler.setProcessInputStream(os);
			} catch (Exception e) {
			}
		});
	}

	@Override
	public void setProcessErrorStream(InputStream is) throws IOException {
		handler.stream().forEach(handler -> {
			try {
				handler.setProcessErrorStream(is);
			} catch (Exception e) {
			}
		});
	}

	@Override
	public void setProcessOutputStream(InputStream is) throws IOException {
		handler.stream().forEach(handler -> {
			try {
				handler.setProcessOutputStream(is);
			} catch (Exception e) {
			}
		});
	}

	@Override
	public void start() throws IOException {
		handler.stream().forEach(handler -> {
			try {
				handler.start();
			} catch (Exception e) {
			}
		});
	}

	@Override
	public void stop() throws IOException {
		handler.stream().forEach(handler -> {
			try {
				handler.stop();
			} catch (Exception e) {
			}
		});
	}
}

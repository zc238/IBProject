package sep.pack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;

public class OrderProcessor extends ApiController{
	public OrderProcessor(IConnectionHandler handler, ILogger inLogger, ILogger outLogger) {
		super(handler, inLogger, outLogger);
		
	}
}
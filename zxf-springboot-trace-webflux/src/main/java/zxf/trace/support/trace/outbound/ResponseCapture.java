package zxf.trace.support.trace.outbound;

import lombok.Data;

@Data
public class ResponseCapture {
    private volatile String status;
    private volatile String headers;
    private volatile String bodyString = "(unavailable)";
}

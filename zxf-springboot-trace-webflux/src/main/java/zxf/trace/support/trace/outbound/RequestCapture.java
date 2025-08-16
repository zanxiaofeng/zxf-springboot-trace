package zxf.trace.support.trace.outbound;

import lombok.Data;

/**
 * 用于捕获请求/响应体的接口
 */

@Data
public class RequestCapture {
    private volatile String path;
    private volatile String method;
    private volatile String headers;
    private volatile String bodyString = "(unavailable)";
}
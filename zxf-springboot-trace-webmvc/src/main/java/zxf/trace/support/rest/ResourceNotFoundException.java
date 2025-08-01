package zxf.trace.support.rest;

/**
 * 资源未找到异常
 *
 * 当请求的资源不存在时抛出此异常。
 * 此异常将被GlobalExceptionHandler捕获并转换为HTTP 404响应。
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 创建一个新的资源未找到异常
     *
     * @param message 异常消息
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 创建一个新的资源未找到异常
     *
     * @param message 异常消息
     * @param cause 原始异常
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用资源类型和ID创建一个新的资源未找到异常
     *
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @return 资源未找到异常
     */
    public static ResourceNotFoundException create(String resourceType, Object resourceId) {
        return new ResourceNotFoundException(
            String.format("Resource %s with id %s not found", resourceType, resourceId));
    }
}

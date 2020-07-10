package sk.mlobb.authserver.app.filter;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class RawRequestResponseLogger extends OncePerRequestFilter {

    private static final List<MediaType> VISIBLE_TYPES = Arrays.asList(
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.MULTIPART_FORM_DATA,
            MediaType.valueOf("text/*"),
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (isAsyncDispatch(request)) {
            chain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), chain);
        }
    }

    protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            beforeRequest(request);
            filterChain.doFilter(request, response);
        } finally {
            afterRequest(response);
            response.copyBodyToResponse();
        }
    }

    protected void beforeRequest(ContentCachingRequestWrapper request) {
        if (log.isInfoEnabled()) {
            String header = logRequestHeader(request);
            String requestLog = logRequestBody(request, header);
            log.info("Received Request:\n{}", requestLog);
        }
    }

    private static String logRequestHeader(ContentCachingRequestWrapper request) {
        val queryString = request.getQueryString();
        StringBuilder stringBuilder = new StringBuilder();
        if (queryString == null) {
            stringBuilder.append(request.getMethod()).append(" ").append(request.getRequestURI());
        } else {
            stringBuilder.append(request.getMethod()).append(" ").append(request.getRequestURI()).append("?")
                    .append(queryString);
        }
        stringBuilder.append("\n").append("Headers: ");
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                Collections.list(request.getHeaders(headerName)).forEach(headerValue ->
                        stringBuilder.append(headerName).append(" : ").append(headerValue).append("; ")));
        return stringBuilder.toString();
    }

    private static String logRequestBody(ContentCachingRequestWrapper request, String header) {
        val content = request.getContentAsByteArray();
        StringBuilder stringBuilder = new StringBuilder(header);
        stringBuilder.append("\nBody: ");
        if (content.length > 0) {
            return logContent(content, request.getContentType(), request.getCharacterEncoding(), stringBuilder);
        }
        return stringBuilder.toString();
    }

    protected void afterRequest(ContentCachingResponseWrapper response) {
        if (log.isInfoEnabled()) {
            String responseLog = logResponse(response);
            log.info("Response: {}", responseLog);
        }
    }

    private static String logResponse(ContentCachingResponseWrapper response) {
        val status = response.getStatus();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(status).append(" ").append(HttpStatus.valueOf(status).getReasonPhrase())
                .append(" ").append("\nHeader: ");
        response.getHeaderNames().forEach(headerName ->
                response.getHeaders(headerName).forEach(headerValue ->
                        stringBuilder.append(headerName).append(" : ").append(headerValue).append("; ")));
        val content = response.getContentAsByteArray();
        stringBuilder.append("\nBody: ");
        if (content.length > 0) {
            return logContent(content, response.getContentType(), response.getCharacterEncoding(), stringBuilder);
        }
        return stringBuilder.toString();
    }

    private static String logContent(byte[] content, String contentType, String contentEncoding,
                                     StringBuilder stringBuilder) {
        val mediaType = MediaType.valueOf(contentType);
        val visible = VISIBLE_TYPES.stream().anyMatch(visibleType -> visibleType.includes(mediaType));
        if (visible) {
            try {
                val contentString = new String(content, contentEncoding);
                Stream.of(contentString.split("\r\n|\r|\n")).forEach(stringBuilder::append);
            } catch (UnsupportedEncodingException e) {
                log.info("[{} bytes content]", content.length);
            }
        } else {
            log.info("[{} bytes content]", content.length);
        }
        return stringBuilder.toString();
    }

    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        } else {
            return new ContentCachingRequestWrapper(request);
        }
    }

    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }
}

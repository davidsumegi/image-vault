package hu.ponte.imagevault.rest;

import hu.ponte.imagevault.exception.UploadException;
import hu.ponte.imagevault.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Upload images")
public class UploadEndpoints {

    @Autowired
    private UploadService uploadService;

    @Operation(
            summary = "Upload a single image",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = "multipart/form-data"
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "File processed")
            })
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadFile(@Parameter(name = "file", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
                                          @RequestParam MultipartFile file) {
        var result = "OK";
        try {
            uploadService.resizeAndSaveToDb(file);
        } catch (UploadException e) {
            result = e.getErrorType().name();
        }
        return Map.of(file.getOriginalFilename(), result);
    }

    @Operation(
            deprecated = true,
            summary = "Upload multiple images (Swagger does not support it)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Files processed")
            })
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadFiles(@RequestParam MultipartFile[] files) {
        var results = new HashMap<String, String>();
        for (var file : files) {
            var result = "OK";
            try {
                uploadService.resizeAndSaveToDb(file);
            } catch (UploadException e) {
                result = e.getErrorType().name();
            }
            results.put(file.getOriginalFilename(), result);
        }
        return results;
    }

}

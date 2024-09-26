package hu.ponte.imagevault.rest;

import hu.ponte.imagevault.service.DownloadService;
import hu.ponte.imagevault.util.FileUtil;
import hu.ponte.imagevault.util.ZipUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@Tag(name = "Download images")
public class DownloadEndpoints {

    @Autowired
    private DownloadService downloadService;

    @Operation(summary = "Download a single image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image returned",
                    content = @Content(schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @GetMapping("/file/{fileName}")
    public ResponseEntity<byte[]> downloadSingleFile(@PathVariable String fileName) {
        var maybeFileFromDb = downloadService.readFromDb(fileName);
        if (maybeFileFromDb.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
        var headers = new HttpHeaders();
        headers.setContentType(FileUtil.typeOf(fileName));
        return new ResponseEntity<>(maybeFileFromDb.get().getContent(), headers, HttpStatus.OK);
    }

    @Operation(summary = "Download all images zipped")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zip returned",
                    content = @Content(schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "404", description = "No images found")
    })
    @GetMapping("/files")
    public ResponseEntity<byte[]> getAllFiles(@RequestParam(required = false) Integer page) {
        var filesFromDb = downloadService.readAllFromDb(page == null ? 0 : page);
        if (filesFromDb.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No images found");
        }
        var zippedContent = ZipUtil.zipFiles(filesFromDb);
        var headers = new HttpHeaders();
        headers.setContentLength(zippedContent.length);
        headers.setContentDispositionFormData("attachment", "images.zip");
        return new ResponseEntity<>(zippedContent, headers, HttpStatus.OK);
    }

}

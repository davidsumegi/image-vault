# Image Vault

Backend server application exposing REST API to upload and download images.

The images are resized (if needed), stored in a PostgreSQL database in an encrypted form.

The uploaded images can then be downloaded individually or zipped.

## How to run the service

Make sure these are already installed:

- Java 17+
- Maven 3.6+
- ImageMagick 7.1+

Run the application specifying the database connection parameters and the ImageMagick `convert` path:

```bash
mvn clean install
java -jar target/image-vault-1.0.jar \
--spring.datasource.url=jdbc:postgresql://localhost:5432/{db} \
--spring.datasource.username={login} \
--spring.datasource.password={pass} \
--imagemagick.searchpath={path}
```

Altrenatively, use`spring-boot:run` to enjoy hot reload during development:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=" \
--spring.datasource.url=jdbc:postgresql://localhost:5432/{db} \
--spring.datasource.username={login} \
--spring.datasource.password={pass} \
--imagemagick.searchpath=/path/to/imagemagick={path} \
"
```

Open Swagger UI at http://localhost:8080/swagger-ui

## Key features

- Upload a file via `POST /api/file`
    - Only JPG and PNG files are accepted with correct content
    - Images not fitting into 5000x5000 pixels are resized
    - Images are stored in a database using AES encryption
- Upload multiple files via `POST /api/files`
    - Cannot test it in Swagger UI because
      it's not [supported](https://github.com/OAI/OpenAPI-Specification/issues/254)
- The upload endpoints will return `200 OK` with a JSON response containing the file names and the results, e.g.

```json
{
  "good.jpg": "OK",
  "image": "MISSING_EXTENSION",
  "bad.pdf": "UNSUPPORTED_EXTENSION",
  "hacked.jpg": "EMPTY_FILE",
  "corrupted.jpg": "WRONG_CONTENT"
}
```

- Download an image via `GET /api/file/{fileName}`
    - It returns `404 Not found` if the image is not present in the database, otherwise `200 OK` with the image
- Download all images in a zip file via `GET /api/files`
    - It returns `404 Not found` if no images exist in the database, otherwise `200 OK` with the zip file
- If more than 100 images are stored already, they can be downloaded in batches with `GET /api/files?page={page}`
    - If `page` is not given or `0`, the first 100 images are returned lexicographically
    - If `page` is `1`, the next 100 images are returned, and so on
    - If `page` is out of bounds, `404 Not found` is returned

## Restrictions

- Only one image can be stored with the same name
    - If an image with the same name is uploaded, the previous one is replaced (unless the new file is erroneous)
- Size of uploaded images in one request must not exceed 100 MB in total, or 20 MB per file
    - `413 Payload Too Large` is returned in case of exceeding either of the thresholds

## Development notes

- The `ResizeService` interface has an implementation that uses ImageMagick
    - The path of the `convert` command needs to be passed as an argument
    - This implementation can be replaced by alternative resizers without touching other classes
- The thresholds can be overruled with `--` in the runner command or in `application.properties`:
    - `spring.servlet.multipart.maxFileSize`
    - `spring.servlet.multipart.maxRequestSize`
    - `download.page.size`
- An existing Postgres DB is required, otherwise the application will fail to start
    - However, the `file` table is created automatically on startup if it doesn't exist
- IMPORTANT! When storing the first image, a `secretKey` file is generated in the root for the encryption
    - This file must not be deleted, otherwise all stored images will be unrecoverable
    - If the file is lost, the `file` table should be purged

## Exception handling

- User issues:
    - The <i>checked</i> `UploadException` contains an `ErrorType` referring
      to the nature of the problem
    - These are returned in a JSON response already described above
- Server issues:
    - If the server fails to process correct user input files, <i>runtime</i> exceptions are thrown
    - In these cases `500 Internal Server Error` status is returned
    - Possible types are `ResizeException`, `EncryptionException` and `ZipException`

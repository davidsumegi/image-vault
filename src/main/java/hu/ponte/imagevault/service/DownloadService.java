package hu.ponte.imagevault.service;

import hu.ponte.imagevault.db.FileRepository;
import hu.ponte.imagevault.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DownloadService {

    @Value("${download.page.size}")
    private int pageSize;

    @Autowired
    private FileRepository fileRepository;


    public Optional<File> readFromDb(String fileName) {
        return fileRepository.findById(fileName);
    }

    public List<File> readAllFromDb(int page) {
        var pageAble = PageRequest.of(page, pageSize, Sort.by("name").ascending());
        return fileRepository.findAll(pageAble).getContent();
    }

}

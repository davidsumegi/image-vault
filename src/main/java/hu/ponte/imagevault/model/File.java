package hu.ponte.imagevault.model;

import hu.ponte.imagevault.db.FileContentConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class File {

    public static Set<String> SUPPORTED_EXTENSIONS = Set.of(".jpg", ".png");

    @Id
    private String name;

    @Lob
    @Convert(converter = FileContentConverter.class)
    private byte[] content;

}

package in.dnsl.domain.result;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AppFileListResult {

    private String lastRev;

    private int count;

    private List<AppFileEntity> fileList;

}

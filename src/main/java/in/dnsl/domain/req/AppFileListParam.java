package in.dnsl.domain.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppFileListParam {

    private int familyId;

    private String fileId;

    private int orderBy;

    private String orderSort;

    @Builder.Default
    private int pageNum = 1;

    @Builder.Default
    private int pageSize = 60;

    private boolean constructPath;

}

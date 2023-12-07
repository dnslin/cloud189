package in.dnsl.domain.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppFileListParam {

    @Builder.Default
    private int familyId = 0;

    private String fileId;

    private int orderBy;

    @Builder.Default
    private String orderSort = "false";

    @Builder.Default
    private int pageNum = 1;

    @Builder.Default
    private int pageSize = 60;

    @Builder.Default
    private boolean constructPath = false;

}

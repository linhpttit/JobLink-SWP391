
package com.joblink.joblink.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CVExport {
    private Integer exportId;
    private Integer seekerId;
    private Integer templateId;
    private String fileName;
    private String filePath;
    private Integer fileSizeKb;
    private LocalDateTime exportedAt;
}

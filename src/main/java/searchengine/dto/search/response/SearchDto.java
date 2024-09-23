package searchengine.dto.search.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchDto {
    private boolean result;
    private String error;
    private Integer count;

    @JsonProperty(value = "data")
    private List<DataDto> dataDtoList;
}

package istad.co.projectservice.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CustomPage<T> {

    private Boolean next;
    private Boolean previous;
    private int total;
    private Long totalElements;
    private List<T> results;

}
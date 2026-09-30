package com.team.cosmocats.model;

import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class Category {
    private Long id;
    @NonNull
    @Setter
    private String name;
}

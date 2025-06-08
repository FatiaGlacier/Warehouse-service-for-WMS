package com.fatia.warehouseservice.responses;

import com.fatia.warehouseservice.models.ShelfModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateShelfReponse {
    private ShelfModel shelf;
    private String status;
    private List<String> warnings;
}

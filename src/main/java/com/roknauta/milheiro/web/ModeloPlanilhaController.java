package com.roknauta.milheiro.web;

import com.roknauta.milheiro.service.PlanilhaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

@RestController
public class ModeloPlanilhaController {

    private final PlanilhaService service;

    public ModeloPlanilhaController(PlanilhaService service) {
        this.service = service;
    }

    @GetMapping("/modelo-importacao.xlsx")
    public ResponseEntity<byte[]> modelo() {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Modelo-Milheiro.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(service.modelo());
    }
}

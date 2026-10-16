package dev.imjaewoo.rush.experiment;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @PostMapping("/api/admin/experiments")
    public ExperimentResult run(@Valid @RequestBody ExperimentRequest request) throws InterruptedException {
        return experimentService.run(request);
    }
}

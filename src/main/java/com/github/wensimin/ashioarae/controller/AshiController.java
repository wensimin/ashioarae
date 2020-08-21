package com.github.wensimin.ashioarae.controller;

import com.github.wensimin.ashioarae.entity.AshiData;
import com.github.wensimin.ashioarae.entity.AshiTarget;
import com.github.wensimin.ashioarae.service.AshiService;
import com.github.wensimin.ashioarae.service.enums.AshiType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("ashi")
public class AshiController {
    private final AshiService ashiService;

    @Autowired
    public AshiController(AshiService ashiService) {
        this.ashiService = ashiService;
    }

    @PostMapping
    public AshiTarget addAshi(@Valid @RequestBody AshiTarget ashiTarget, Principal principal) {
        return ashiService.save(ashiTarget,principal.getName());
    }

    @PutMapping("head/{type}")
    public void ashiHead(@PathVariable AshiType type,Principal principal){
        ashiService.updateAshiHead(type,principal.getName());

    }
    @PutMapping("nick/{type}")
    public void ashiNick(@PathVariable AshiType type,Principal principal){
        ashiService.updateAshiNick(type,principal.getName());

    }
    @GetMapping("{type}")
    public AshiData ashiInfo(@PathVariable AshiType type,Principal principal){
        return ashiService.ashiInfo(type,principal.getName());
    }
    @GetMapping
    public AshiData ashiInfo(Principal principal){
        return ashiService.ashiInfo(principal.getName());
    }
}

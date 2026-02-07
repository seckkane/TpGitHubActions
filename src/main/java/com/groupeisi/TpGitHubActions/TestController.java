package com.groupeisi.TpGitHubActions;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "test", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
public class TestController {

    @GetMapping
    public List<Object> search() {
       return List.of( new Avis(1, "Meilleure Formation DevOps", 1),
                       new Avis(2, "On attend la partie GitLab", 1),
                       new Avis(3, "La partie Git Hub actions est bouclée", 0));
    }
}

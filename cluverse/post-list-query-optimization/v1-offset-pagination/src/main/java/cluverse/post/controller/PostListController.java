package cluverse.post.controller;

import cluverse.post.service.PostListQueryService;
import cluverse.post.service.request.PostListRequest;
import cluverse.post.service.response.PostPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostListController {

    private final PostListQueryService postListQueryService;

    @GetMapping
    public PostPageResponse getPosts(@Valid @ModelAttribute PostListRequest request) {
        return postListQueryService.getPosts(request);
    }
}

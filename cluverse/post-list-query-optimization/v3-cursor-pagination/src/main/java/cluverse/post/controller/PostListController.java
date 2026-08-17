package cluverse.post.controller;

import cluverse.post.service.PostListQueryService;
import cluverse.post.service.request.PostCursorRequest;
import cluverse.post.service.response.PostCursorPageResponse;
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
    public PostCursorPageResponse getPosts(@Valid @ModelAttribute PostCursorRequest request) {
        return postListQueryService.getPosts(request);
    }
}

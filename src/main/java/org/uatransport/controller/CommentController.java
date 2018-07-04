package org.uatransport.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uatransport.entity.Comment;
import org.uatransport.service.CommentService;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

//    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{transitId}")
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment,
                                              @PathVariable Integer transitId,
                                              @RequestParam(value = "userId") Integer userId,
                                              @RequestParam(value = "parentId", defaultValue = "") Integer parentId) {
        return new ResponseEntity<>(commentService.add(comment, transitId, userId, parentId), HttpStatus.CREATED);
    }

    //test
    @GetMapping("/{id}")
    public Comment getOne(@PathVariable Integer id) {
        return commentService.getById(id);
    }
}

package org.uatransport.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uatransport.entity.Comment;
import org.uatransport.entity.dto.CommentDTO;
import org.uatransport.entity.dto.CommentRatingDTO;
import org.uatransport.entity.dto.UserInfo;
import org.uatransport.service.CommentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ModelMapper modelMapper;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@RequestBody Comment comment,
            @RequestParam(value = "transitId") Integer transitId, @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "parentId", required = false) Integer parentId) {
        Comment addedComment = commentService.add(comment, transitId, userId, parentId);
        return new ResponseEntity<>(modelMapper.map(addedComment, CommentDTO.class), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<CommentDTO> addImageToComment(@RequestParam("commentId") Integer id,
            @RequestBody String images) {
        Comment updatedComment = commentService.addPics(images, id);
        return new ResponseEntity<>(modelMapper.map(updatedComment, CommentDTO.class), HttpStatus.OK);
    }

    @GetMapping("/{transitId}")
    public List<CommentDTO> getTransitComments(@PathVariable Integer transitId) {
        return commentService.getAllTopLevel(transitId).stream()
                .map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
    }

    @GetMapping(params = "parentId")
    public List<CommentDTO> getChildComments(@RequestParam(value = "parentId") Integer parentId) {
        return commentService.getAllByParentId(parentId).stream()
                .map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
    }

    @GetMapping(params = "userId")
    public List<CommentDTO> getUserComments(@RequestParam(value = "userId") Integer userId) {
        return commentService.getAllByUserId(userId).stream().map(comment -> modelMapper.map(comment, CommentDTO.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/{commentId}/voted")
    public List<UserInfo> getVotedUsers(@PathVariable Integer commentId) {
        return commentService.getAllByVotedComment(commentId).stream().map(user -> modelMapper.map(user, UserInfo.class))
            .collect(Collectors.toList());
    }

    @PostMapping("/{commentId}/like/{userId}")
    public ResponseEntity<CommentRatingDTO> like(@PathVariable Integer commentId,
                                                 @PathVariable Integer userId) {
        CommentRatingDTO rating = modelMapper.map(commentService.vote(commentId, userId, true), CommentRatingDTO.class);
        return new ResponseEntity<>(rating, HttpStatus.OK);
    }

    @PostMapping("/{commentId}/dislike/{userId}")
    public ResponseEntity<CommentRatingDTO> dislike(@PathVariable Integer commentId,
                                                    @PathVariable Integer userId) {
        CommentRatingDTO rating = modelMapper.map(commentService.vote(commentId, userId, false), CommentRatingDTO.class);
        return new ResponseEntity<>(rating, HttpStatus.OK);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(@RequestBody Comment comment, @PathVariable Integer commentId) {
        Comment updatedComment = commentService.update(comment, commentId);

        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Integer commentId) {
        commentService.delete(commentId);
    }
}

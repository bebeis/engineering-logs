package cluverse.meta.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "post_view_count")
@Getter
public class PostViewCount {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Column(nullable = false)
    private int viewCount;

    protected PostViewCount() {
    }
}

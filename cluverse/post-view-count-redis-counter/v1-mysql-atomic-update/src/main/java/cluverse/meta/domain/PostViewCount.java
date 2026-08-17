package cluverse.meta.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_view_count")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostViewCount {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Column(nullable = false)
    private int viewCount;
}

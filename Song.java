package player.media.com.funcionara;

public class Song {

    // Custom attribs
    private Long specifiedDuration;
    private String shuffleIndex;

    // Basic attribs
    private String name;
    private Integer duration;
    private String author;
    private Integer id;

    Song(Integer id, Integer duration, String name, String author) {
        this.id = id;
        this.duration = duration;
        this.name = name;
        this.author = author;
    }

    public Long getSpecifiedDuration() {
        return specifiedDuration;
    }

    public void setSpecifiedDuration(Long specifiedDuration) {
        this.specifiedDuration = specifiedDuration;
    }

    public String getShuffleIndex() {
        return shuffleIndex;
    }

    public void setShuffleIndex(String shuffleIndex) {
        this.shuffleIndex = shuffleIndex;
    }

    public String getName() {
        return name;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getAuthor() {
        return author;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Song{" +
                "specifiedDuration=" + specifiedDuration +
                ", shuffleIndex='" + shuffleIndex + '\'' +
                ", name='" + name + '\'' +
                ", duration=" + duration +
                ", author='" + author + '\'' +
                ", id=" + id +
                '}';
    }
}

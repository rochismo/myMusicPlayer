package player.media.com.funcionara;

public class Song {
    // Custom attribs
    private Long specifiedDuration;
    private String shuffleIndex;

    // Basic attribs
    private String name;
    private String duration;
    private String author;

    public Song(String duration, String name, String author) {
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
}

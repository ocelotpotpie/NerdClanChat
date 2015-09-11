package nu.nerd.NerdClanChat.database;

import com.avaje.ebean.validation.NotNull;

import javax.persistence.*;


@Entity()
@Table(name = "clanchat_members")
public class ChannelMember {

    @Id
    private Integer id;

    @NotNull
    private String channel;

    @NotNull
    private String UUID;

    private boolean manager;

    private boolean subscribed;


    public ChannelMember() {
    }

    public ChannelMember(String channel, String UUID, boolean isManager) {
        this.setChannel(channel);
        this.setUUID(UUID);
        this.setSubscribed(true);
        this.setManager(isManager);
    }


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUUID() {
        return this.UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public boolean isManager() {
        return this.manager;
    }

    public void setManager(boolean isManager) {
        this.manager = isManager;
    }

    public boolean isSubscribed() {
        return this.subscribed;
    }

    public void setSubscribed(boolean isSubscribed) {
        this.subscribed = isSubscribed;
    }


}

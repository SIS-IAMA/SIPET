package iama.sipet.response;

import java.util.List;

import iama.sipet.entity.UserEntity;

public class UserResponse {
    private List<UserEntity> user;

    public List<UserEntity> getUser() {
        return user;
    }

    public void setUser(List<UserEntity> user) {
        this.user = user;
    }
}

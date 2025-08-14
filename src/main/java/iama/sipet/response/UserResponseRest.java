package iama.sipet.response;

public class UserResponseRest extends ResponseRest {

    private UserResponse userResponse = new UserResponse();

    public UserResponse getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(UserResponse userResponse) {
        this.userResponse = userResponse;
    }
    
}

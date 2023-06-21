package com.project.livechat.domain.usecases

import com.project.livechat.domain.models.User

class GetRegisteredUsersUseCase(
    val usersRepository: UsersRepository
) {
    operator fun invoke() {
        usersRepository.getUsers()
    }
}

class UsersRepository(
    val usersRemoteData: UsersRemoteData,
    val usersLocalData: UsersLocalData
) : IUsersRepository {
    override fun getUsers(): List<User> {
        return usersRemoteData.getUsers()
    }
}

interface IUsersRepository {
    fun getUsers(): List<User>
}

class UsersRemoteData(

) : IUsersRemoteData {
    override fun getUsers(): List<User> {
        return listOf(
            UserResponse(
                name = "Luis",
                phoneNo = "+5521985670564",
                description = "A nice guy",
                photo = "https://www.w3schools.com/howto/img_avatar.png"
            )
        ).map { it.toUser() }
    }
}

interface IUsersRemoteData {
    fun getUsers(): List<User>
}

class UserResponse(
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null
) {
    fun toUser(): User {
        return with(this) {
            User(
                name = name,
                phoneNo = phoneNo,
                description = description,
                photo = photo
            )
        }
    }
}

class UsersLocalData(

) {

}
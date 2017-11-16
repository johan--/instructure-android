package com.instructure.dataseeding

import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.CanvasUser
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.soseedy.CreateTeacherRequest
import com.instructure.soseedy.SoSeedyGrpc
import com.instructure.soseedy.Teacher
import io.grpc.stub.StreamObserver

class SoSeedyImpl : SoSeedyGrpc.SoSeedyImplBase() {

    private fun createTeacher(): CanvasUser {
        val user = UserApi.createRandomTeacher().body()
        val course = CoursesApi.createRandomCourse().body()
        EnrollmentsApi.enrollTeacher(course.id, user.id)
        return user
    }

    override fun createTeacher(request: CreateTeacherRequest?, responseObserver: StreamObserver<Teacher>?) {
        val teacher = createTeacher()
        val reply = Teacher.newBuilder()
                .setId(teacher.id.toInt())
                .setUsername(teacher.loginId)
                .setPassword(teacher.password)
                .setDomain(CanvasRestAdapter.canvasDomain)
                .setToken(teacher.token)
                .setName(teacher.name)
                .setShortName(teacher.shortName)
                .setAvatarUrl(teacher.avatarUrl.orEmpty())
                .build()

        responseObserver?.onNext(reply)
        responseObserver?.onCompleted()
    }
}

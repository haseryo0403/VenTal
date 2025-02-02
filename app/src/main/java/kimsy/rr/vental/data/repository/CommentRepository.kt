package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kimsy.rr.vental.data.Comment
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommentRepository @Inject constructor(
    private val db: FirebaseFirestore
){
    suspend fun fetchComments(
        posterId: String,
        swipeCardId: String,
        debateId: String
    ): List<Comment> {
        val docRef = db
            .collection("users")
            .document(posterId)
            .collection("swipeCards")
            .document(swipeCardId)
            .collection("debates")
            .document(debateId)
            .collection("comments")
            .orderBy("commentedDateTime", Query.Direction.DESCENDING)

        val querySnapshot = docRef.get().await()
        val comments = querySnapshot.documents.mapNotNull { document ->
            val comment = document.toObject(Comment::class.java)
            val commentedTime = document.getTimestamp("commentedDateTime")?.toDate()
            comment?.copy(commentedDateTime = commentedTime)
        }
        return comments
    }

    suspend fun saveComment (
        posterId: String,
        swipeCardId: String,
        debateId: String,
        comment: Comment
    ) {
        val docRef = db
            .collection("users")
            .document(posterId)
            .collection("swipeCards")
            .document(swipeCardId)
            .collection("debates")
            .document(debateId)
            .collection("comments")

        val commentDoc = docRef.document()
        val commentWithId = comment.copy(commentId = commentDoc.id)

        commentDoc.set(commentWithId).await()
    }
}
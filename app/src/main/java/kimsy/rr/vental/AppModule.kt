package kimsy.rr.vental

import android.app.Application
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.ImageRepository
import kimsy.rr.vental.data.repository.UserRepository
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance("gs://vental-4eb3c.firebasestorage.app")
    }

    @Provides
    fun provideStorageReference(
        storage: FirebaseStorage
    ) : StorageReference {
        return storage.reference
    }

    @Provides
    @Singleton
    fun provideGoogleSignInClient(application: Application): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(application, gso)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        googleSignInClient: GoogleSignInClient,
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ): UserRepository {
        return UserRepository(googleSignInClient,firebaseAuth, firebaseFirestore)
    }

    @Provides
    @Singleton
    fun provideImageRepository(
        storageReference: StorageReference
    ): ImageRepository{
        return ImageRepository(storageReference)
    }
    @Provides
    @Singleton
    fun provideVentCardRepository(
        firebaseFirestore: FirebaseFirestore
    ): VentCardRepository{
        return VentCardRepository(firebaseFirestore)
    }
    @Provides
    @Singleton
    fun provideDebateRepository(
        firebaseFirestore: FirebaseFirestore,
    ): DebateRepository{
        return DebateRepository(firebaseFirestore)
    }

    @Provides
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }

//    @Provides
//    @Singleton
//    fun provideBaseUseCase(networkUtils: NetworkUtils, loadCurrentUserUseCase: LoadCurrentUserUseCase): BaseUseCase {
//        return BaseUseCase(networkUtils, loadCurrentUserUseCase)
//    }



//    @Provides
//    @Singleton
//    fun provideImageUtils(@ApplicationContext context: Context): ImageUtils {
//        return ImageUtils(context) // ImageUtilsのインスタンスを提供
//    }
}
import kotlinx.serialization.Serializable
import com.example.dodojob.data.supabase.ProvideSupabase

val supabase = ProvideSupabase.client
@Serializable
data class UserNameRow(val name: String?)

suspend fun fetchUserName(userId: String): String? {
    val row = supabase.postgrest["users"]     // ← 여러분 테이블명으로 맞추세요
        .select(columns = "name")
        .eq("id", userId)
        .single()
        .decodeAs<UserNameRow>()
    return row.name
}
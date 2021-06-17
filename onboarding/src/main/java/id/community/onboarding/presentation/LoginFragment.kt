package id.community.onboarding.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.coroutines.toDeferred
import id.community.gowes.LoginMutation
import id.community.gowes.type.CustomType
import id.community.onboarding.databinding.FragmentLoginBinding
import kotlinx.coroutines.*
import okhttp3.OkHttpClient

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private var supervisorJob = SupervisorJob()

    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        Log.e(LoginFragment::class.java.simpleName, "An error happened: $e")
    }

    private val objectIdTypeAdapter = object : CustomTypeAdapter<String> {
        override fun encode(value: String) = CustomTypeValue.GraphQLString(value)
        override fun decode(value: CustomTypeValue<*>) = value.value.toString()
    }


    private fun initView() {
        with(binding) {
            loginEditText.addTextChangedListener(loginEditTextWatcher)
            passwordEditText.addTextChangedListener(passwordEditTextWatcher)
            loginButton.setOnClickListener {
                login(loginEditText.text.toString(), passwordEditText.text.toString())
            }
        }
    }

    private fun login(username: String, password: String) {
        val apolloClient = ApolloClient.builder()
            .okHttpClient(OkHttpClient.Builder().build())
            .serverUrl("https://gowes-graphql.herokuapp.com/")
            .normalizedCache(
                LruNormalizedCacheFactory(
                    EvictionPolicy.builder().maxSizeBytes(10 * 1024 * 1024).build()
                )
                    .chain(SqlNormalizedCacheFactory(requireContext(), "apollo.db"))
            )
            .addCustomTypeAdapter(CustomType.ID, objectIdTypeAdapter)
            .build()

        CoroutineScope(Dispatchers.IO).launch(getJobErrorHandler() + supervisorJob) {
            val mutation = LoginMutation(username = username, password = password)
            val response = apolloClient.mutate(mutation).toDeferred().await()

            if (response.hasErrors()) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        requireContext(),
                        "Failed to login: ${(response.errors?.get(0)?.message ?: "")}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Log.d("MainActivity", "Succeeded to login, token: ${response.data?.login?.token}")
            }
        }
    }

    private val loginEditTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            binding.passwordError.visibility = View.GONE
            binding.loginLabel.visibility = if (s.toString().isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private val passwordEditTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            binding.passwordError.visibility = View.GONE
            binding.passwordLabel.visibility = if (s.toString().isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

}

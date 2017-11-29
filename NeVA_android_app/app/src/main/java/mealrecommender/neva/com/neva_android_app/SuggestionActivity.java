package mealrecommender.neva.com.neva_android_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import neva.backend.BackendGrpc;
import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;

public class SuggestionActivity extends AppCompatActivity {

    EditText suggestion_box;
    Button suggest_button;
    ByteString loginToken;
    ManagedChannel mChannel;
    BackendGrpc.BackendBlockingStub blockingStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        suggest_button = findViewById(R.id.suggest);
        suggestion_box = findViewById(R.id.suggestion);

        Intent intent = getIntent();
        byte[] loginTokenArray = intent.getByteArrayExtra(LoginActivity.TOKEN_EXTRA);
        loginToken = ByteString.copyFrom(loginTokenArray);

        mChannel = ManagedChannelBuilder.forAddress("www.0xdeffbeef.com", 50051).usePlaintext(true).build();
        blockingStub = BackendGrpc.newBlockingStub(mChannel);
    }

    public void onSuggestClick(View view)
    {
        Log.d("SUGG_Click", "Enter");
        String suggestionText = suggestion_box.getText().toString();
        Log.d("SUGG_Click", "Get Text");
        suggest_button.setEnabled(false);
        Log.d("SUGG_Click", "ButtonDisabled");
        SuggestionOuterClass.Suggestion suggestion;
        suggestion = SuggestionOuterClass.Suggestion.newBuilder()
                    .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
                    .setName(suggestionText)
                    .build();
        Log.d("SUGG_Click", "SuggestionCreated");
        BackendOuterClass.SuggestionItemPropositionRequest suggestionRequest;
        suggestionRequest = BackendOuterClass.SuggestionItemPropositionRequest.newBuilder()
                            .setToken(loginToken)
                            .setSuggestion(suggestion)
                            .build();
        Log.d("SUGG_Click", "SuggestionRequestCreated");
        try {
            Log.d("SUGG_Click", "Ask");
            BackendOuterClass.GenericReply genRep = blockingStub.suggestionItemProposition(suggestionRequest);
            Log.d("SUGG_Click", "Rec");
            Toast.makeText(getBaseContext(), "Suggested", Toast.LENGTH_SHORT).show();
            Log.d("SUGG_Click", "Toast");
        }
        catch (Exception e)
        {
            Log.d("SUGG_Click", "Exception");
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            suggestion_box.setEnabled(true);
        }

    }
}

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
    EditText tag_suggestion;
    Button suggest_button;
    Button suggest_tag;
    ByteString loginToken;
    ManagedChannel mChannel;
    BackendGrpc.BackendBlockingStub blockingStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        suggest_button = findViewById(R.id.suggest);
        suggestion_box = findViewById(R.id.suggestion);

        tag_suggestion=findViewById(R.id.tag_suggestion);
        suggest_tag = findViewById(R.id.suggest_tag);

        Intent intent = getIntent();
        byte[] loginTokenArray = intent.getByteArrayExtra(LoginActivity.TOKEN_EXTRA);
        loginToken = ByteString.copyFrom(loginTokenArray);

        mChannel = ManagedChannelBuilder.forAddress("www.0xdeffbeef.com", 50051).usePlaintext(true).build();
        blockingStub = BackendGrpc.newBlockingStub(mChannel);
    }

    public void onSuggestClick(View view)
    {

        String suggestionText = suggestion_box.getText().toString();
        suggest_button.setEnabled(false);

        SuggestionOuterClass.Suggestion suggestion;
        suggestion = SuggestionOuterClass.Suggestion.newBuilder()
                    .setSuggestionCategory(SuggestionOuterClass.Suggestion.SuggestionCategory.MEAL)
                    .setName(suggestionText)
                    .build();

        BackendOuterClass.SuggestionItemPropositionRequest suggestionRequest;
        suggestionRequest = BackendOuterClass.SuggestionItemPropositionRequest.newBuilder()
                            .setToken(loginToken)
                            .setSuggestion(suggestion)
                            .build();
        try
        {
            BackendOuterClass.GenericReply genRep = blockingStub.suggestionItemProposition(suggestionRequest);
            Toast.makeText(getBaseContext(), "Suggested", Toast.LENGTH_SHORT).show();
            suggestion_box.setEnabled(true);
        }
        catch (Exception e)
        {
            Log.d("SUGG_Click", "Exception");
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            suggestion_box.setEnabled(true);
        }

    }

    public void onTagSuggestClick(View view)
    {
        String tagString = tag_suggestion.getText().toString();
        suggest_tag.setEnabled(false);

        BackendOuterClass.TagPropositionRequest tagProp;
        tagProp = BackendOuterClass.TagPropositionRequest.newBuilder()
                    .setTag(tagString).setToken(loginToken).build();

        try
        {
            BackendOuterClass.GenericReply genRep = blockingStub.tagProposition(tagProp);
            Toast.makeText(getBaseContext(), "Tag Suggested", Toast.LENGTH_SHORT).show();
            suggest_tag.setEnabled(true);
        }
        catch (Exception e)
        {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            suggest_tag.setEnabled(true);
        }

    }
}

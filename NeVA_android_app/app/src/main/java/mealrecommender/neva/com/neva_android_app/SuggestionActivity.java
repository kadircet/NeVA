package mealrecommender.neva.com.neva_android_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.protobuf.ByteString;

import neva.backend.BackendOuterClass;
import neva.backend.SuggestionOuterClass;

public class SuggestionActivity extends AppCompatActivity {

    EditText suggestion_box;
    Button suggest_button;
    ByteString loginToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        suggest_button = findViewById(R.id.suggest);
        suggestion_box = findViewById(R.id.suggestion);
    }

    public void onSuggestClick(View view)
    {
        String suggestionText = suggestion_box.getText().toString();

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
    }
}

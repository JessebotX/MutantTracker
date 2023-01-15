package ca.cmpt213.asn5.client;

import ca.cmpt213.asn5.client.model.Mutant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {
	private static final String PID_LABEL = "PID: ";
	private static final String NAME_LABEL = "Name: ";
	private static final String WEIGHT_LABEL = "Weight: ";
	private static final String HEIGHT_LABEL = "Height: ";
	private static final String CATEGORY_LABEL = "Category: ";
	private static final String ABILITY_LABEL = "Ability: ";
	private static final String BLANK = "-";
	private static final int SCENE_SIZE = 600;
	private static final int BASE_HEIGHT_MUTANT = 100;
	private static final int BASE_WIDTH_MUTANT = 100;
	private static final int DIVISOR = 5;

	private final Gson gson = new Gson();

	private List<Mutant> mutantList;
	private Stage primaryStage;
	private Button addButton;
	private Button deleteButton;
	private Label errorsDisplay;
	private Label pidDisplay;
	private Label nameDisplay;
	private Label weightDisplay;
	private Label heightDisplay;
	private Label categoryDisplay;
	private Label abilityDisplay;
	private VBox rootVbox;
	private ScrollPane mutantDisplayScrollPane;
	private Scene mainScene;

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage stage) throws IOException {
		primaryStage = stage;
		stage.setTitle("Mutant Tracker");
		redrawMainScene();
	}

	private void redrawMainScene() {
		pidDisplay = new Label(PID_LABEL + BLANK);
		nameDisplay = new Label(NAME_LABEL + BLANK);
		weightDisplay = new Label(WEIGHT_LABEL + BLANK);
		heightDisplay = new Label(HEIGHT_LABEL + BLANK);
		categoryDisplay = new Label(CATEGORY_LABEL + BLANK);
		abilityDisplay = new Label(ABILITY_LABEL + BLANK);
		errorsDisplay = new Label();

		addButton = new Button("Add New Mutant");
		addButton.setOnMouseClicked(e -> redrawAddMutantScene());

		deleteButton = new Button("Delete Selected Mutant");
		deleteButton.setOnMouseClicked(e -> errorsDisplay.setText("Error: You have not selected a mutant yet."));

		getAllMutants();
		redrawMutants();
		rootVbox = new VBox(
				mutantDisplayScrollPane,
				errorsDisplay,
				pidDisplay,
				nameDisplay,
				weightDisplay,
				heightDisplay,
				categoryDisplay,
				abilityDisplay,
				addButton,
				deleteButton
		);
		rootVbox.setAlignment(Pos.CENTER);
		mainScene = new Scene(rootVbox, SCENE_SIZE, SCENE_SIZE);
		primaryStage.setScene(mainScene);
		primaryStage.show();
	}

	private void redrawMutants() {
		getAllMutants();
		VBox mutantDisplayVbox = new VBox();

		for (int i = 0; i < mutantList.size(); i++) {
			final int INDEX = i;
			Mutant mutant = mutantList.get(i);

			Label mutantName = new Label(mutant.getName());
			Rectangle mutantShape = new Rectangle();
			mutantShape.setHeight(BASE_HEIGHT_MUTANT + (mutant.getHeight() / DIVISOR));
			mutantShape.setWidth(BASE_WIDTH_MUTANT + (mutant.getWeight() / DIVISOR));

			if (i % 2 == 0) {
				mutantShape.setFill(Color.BLUE);
			}

			VBox mutantBox = new VBox(mutantName, mutantShape);
			mutantBox.setOnMouseClicked(e -> displayInfoAndDeleteButton(mutant));
			mutantBox.setOnMouseEntered(e -> {
				mutantShape.setFill(Color.RED);
			});
			mutantBox.setOnMouseExited(e -> {
				if (INDEX % 2 == 0) {
					mutantShape.setFill(Color.BLUE);
				} else {
					mutantShape.setFill(Color.BLACK);
				}
			});
			mutantDisplayVbox.getChildren().add(mutantBox);
		}

		mutantDisplayScrollPane = new ScrollPane(mutantDisplayVbox);
		mutantDisplayScrollPane.setFitToHeight(true);
		mutantDisplayScrollPane.setFitToWidth(true);
		mutantDisplayScrollPane.setMaxWidth(SCENE_SIZE / 2);
	}

	private void displayInfoAndDeleteButton(Mutant mutant) {
		pidDisplay.setText(PID_LABEL + mutant.getPid());
		nameDisplay.setText(NAME_LABEL + mutant.getName());
		weightDisplay.setText(WEIGHT_LABEL + mutant.getWeight());
		heightDisplay.setText(HEIGHT_LABEL + mutant.getHeight());
		categoryDisplay.setText(CATEGORY_LABEL + mutant.getCategory());
		abilityDisplay.setText(ABILITY_LABEL + mutant.getAbility());
		deleteButton.setOnMouseClicked(e -> deleteMutantFromServer(mutant.getPid()));
	}

	private void redrawAddMutantScene() {
		Label nameFieldLabel = new Label(NAME_LABEL);
		TextField nameField = new TextField();
		Label heightFieldLabel = new Label(HEIGHT_LABEL);
		TextField heightField = new TextField();
		Label weightFieldLabel = new Label(WEIGHT_LABEL);
		TextField weightField = new TextField();
		Label categoryFieldLabel = new Label(CATEGORY_LABEL);
		TextField categoryField = new TextField();
		Label abilityFieldLabel = new Label(ABILITY_LABEL);
		TextField abilityField = new TextField();

		Label errorsLabel = new Label();
		Button confirmAddButton = new Button("Confirm Addition");
		confirmAddButton.setOnMouseClicked(e -> {
			if (nameField.getText().isBlank()
					|| heightField.getText().isBlank()
					|| weightField.getText().isBlank()
					|| categoryField.getText().isBlank()
					|| abilityField.getText().isBlank())
			{
				errorsLabel.setText("No field should be left blank");
				return;
			}

			if (!isInt(heightField) || !isInt(weightField) || !isInt(abilityField)) {
				errorsLabel.setText("Weight, Height and Ability fields must be integers");
				return;
			}

			int height = Integer.parseInt(heightField.getText());
			int weight = Integer.parseInt(weightField.getText());
			int ability = Integer.parseInt(abilityField.getText());

			if (height < 0 || weight < 0 || ability < 0) {
				errorsLabel.setText("Weight, Height and Ability fields must be >= 0");
				return;
			}

			if (ability > Mutant.MAX_ABILITY) {
				errorsLabel.setText("Ability must be <= 100");
				return;
			}

			addNewMutantToServer(nameField.getText(), categoryField.getText(), height, weight, ability);

			redrawMainScene();
		});
		confirmAddButton.setAlignment(Pos.CENTER);

		VBox fieldVbox = new VBox(
				nameFieldLabel, nameField,
				heightFieldLabel, heightField,
				weightFieldLabel, weightField,
				categoryFieldLabel, categoryField,
				abilityFieldLabel, abilityField
		);
		VBox buttonBox = new VBox(errorsLabel, confirmAddButton);
		buttonBox.setAlignment(Pos.BOTTOM_CENTER);

		VBox addMutantVbox = new VBox(fieldVbox, buttonBox);

		Scene scene = new Scene(addMutantVbox, SCENE_SIZE, SCENE_SIZE);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void getAllMutants() {
		mutantList = new ArrayList<>();
		String json = "";

		try {
			URL url = new URL("http://localhost:8080/api/mutant/all");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			connection.getInputStream();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(connection.getInputStream())
			);
			String output = br.readLine();
			json = output;

			connection.disconnect();
		} catch (IOException e){
			errorsDisplay.setText("Error: Failed to fetch a list of mutants");
		}

		if (!json.isBlank()) {
			Type type = new TypeToken<List<Mutant>>() {}.getType();
			List<Mutant> mutants = gson.fromJson(json, type);

			if (mutants != null) {
				mutantList = mutants;
			} else {
				mutantList = new ArrayList<>();
			}
		}
	}

	private void addNewMutantToServer(String name, String category, int height, int weight, int ability) {
		try {
			URL url = new URL("http://localhost:8080/api/mutant/add");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");

			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(
					"{\"name\":\"" + name + "\""
					+ ",\"ability\":" + ability
					+ ",\"weight\":" + weight
					+ ",\"height\":" + height
					+ ",\"category\":\"" + category + "\"}"
			);
			wr.flush();
			wr.close();

			connection.connect();
			System.out.println(connection.getResponseCode());
			connection.disconnect();
		} catch (IOException e) {
			errorsDisplay.setText("Error: Failed to add mutant to server");
		}

		redrawMainScene();
	}

	private void deleteMutantFromServer(long id) {
		try {
			URL url = new URL("http://localhost:8080/api/mutant/" + id);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("DELETE");

			connection.getInputStream();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(connection.getInputStream())
			);
			br.readLine();

			connection.disconnect();
			System.out.println("DELETED");
		} catch (IOException e){
			errorsDisplay.setText("Error: Failed to delete selected mutant");
		}

		redrawMainScene();
	}

	private boolean isInt(TextField field) {
		try {
			Integer.parseInt(field.getText());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
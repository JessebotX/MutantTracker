package ca.cmpt213.asn5.server.controller;

import ca.cmpt213.asn5.server.model.Mutant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server controller of mutant data manipulation
 */
@RestController
public class MutantController {
	private static final String MUTANT_DATA_JSON_FILE = "data/mutant.json";
	private static final String DATA_DIRECTORY = "data";

	private final AtomicLong counter = new AtomicLong();
	private final Gson gson = new Gson();

	private List<Mutant> mutants = new ArrayList<>();

	@GetMapping("/api/mutant/all")
	public List<Mutant> getMutants() {
		return this.mutants;
	}

	@GetMapping("/api/mutant/{id}")
	public Mutant getMutant(@PathVariable String id) {
		for (Mutant mutant : mutants) {
			String mutantStringId = Long.toString(mutant.getPid());
			if (id.equalsIgnoreCase(mutantStringId)) {
				return mutant;
			}
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutant with id=" + id + " does not exist");
	}

	@PostMapping("/api/mutant/add")
	public Mutant addPerson(@RequestBody Mutant newMutant, HttpServletResponse response) {
		newMutant.setPid(counter.incrementAndGet());
		mutants.add(newMutant);
		response.setStatus(201);
		writeToFile();
		return newMutant;
	}

	@DeleteMapping("/api/mutant/{id}")
	public void deletePerson(@PathVariable String id, HttpServletResponse response){
		boolean idExists = false;

		for (int i = 0; i < mutants.size(); i++) {
			String mutantStringId = Long.toString(mutants.get(i).getPid());
			if (id.equalsIgnoreCase(mutantStringId)) {
				mutants.remove(i);
				idExists = true;
				break;
			}
		}

		if (idExists) {
			response.setStatus(204);
			writeToFile();
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutant with id=" + id + " does not exist");
		}
	}

	@PostConstruct
	public void init() {
		readFromDataFile();
	}

	private void readFromDataFile() {
		String content;

		try {
			content = Files.readString(Paths.get(MUTANT_DATA_JSON_FILE));
		} catch (IOException e) {
			return; // return if such file does not exist
		}

		Type type = new TypeToken<List<Mutant>>() {}.getType();
		List<Mutant> readMutants = gson.fromJson(content, type);

		if (readMutants != null) {
			mutants = readMutants;
		}

		updateIds();
	}

	private void updateIds() {
		for (Mutant mutant : mutants) {
			mutant.setPid(counter.incrementAndGet());
		}

		writeToFile();
	}

	private void writeToFile() {
		File dataFile = new File(MUTANT_DATA_JSON_FILE);

		try {
			Files.createDirectories(Paths.get(DATA_DIRECTORY));
			Files.createFile(Paths.get(MUTANT_DATA_JSON_FILE));
		} catch (IOException e) {
			// files and directories already exist
		}

		try {
			PrintWriter writer = new PrintWriter(dataFile);
			String mutantJson = gson.toJson(mutants);
			writer.println(mutantJson);
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("File does not exist");
		}
	}
}

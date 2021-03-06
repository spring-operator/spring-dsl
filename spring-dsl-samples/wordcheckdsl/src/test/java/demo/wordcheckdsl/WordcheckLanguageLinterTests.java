/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.wordcheckdsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.dsl.document.Document;
import org.springframework.dsl.document.TextDocument;
import org.springframework.dsl.model.LanguageId;
import org.springframework.dsl.service.reconcile.ReconcileProblem;

/**
 * Tests for {@link WordcheckLanguageLinter}.
 *
 * @author Janne Valkealahti
 *
 */
public class WordcheckLanguageLinterTests {

	@Test
	public void test() {
		WordcheckLanguageLinter linter = new WordcheckLanguageLinter();
		linter.getProperties().setWords(Arrays.asList("jack", "is", "a", "dull", "boy"));

		Document document = new TextDocument("fakeuri", LanguageId.TXT, 0, "");
		List<ReconcileProblem> problems = linter.lint(document).toStream().collect(Collectors.toList());
		assertThat(problems).isEmpty();

		document = new TextDocument("fakeuri", LanguageId.TXT, 0, "xxx");
		problems = linter.lint(document).toStream().collect(Collectors.toList());
		assertThat(problems).hasSize(1);

		document = new TextDocument("fakeuri", LanguageId.TXT, 0, "jack is a dull boy");
		problems = linter.lint(document).toStream().collect(Collectors.toList());
		assertThat(problems).hasSize(0);

		document = new TextDocument("fakeuri", LanguageId.TXT, 0, "jack is a xxx dull xxx boy");
		problems = linter.lint(document).toStream().collect(Collectors.toList());
		assertThat(problems).hasSize(2);

		document = new TextDocument("fakeuri", LanguageId.TXT, 0, "jack is a dull boy\nxxx\njack is a dull boy");
		problems = linter.lint(document).toStream().collect(Collectors.toList());
		assertThat(problems).hasSize(1);
		ReconcileProblem problem = problems.get(0);
		assertThat(problem.getRange().getStart().getLine()).isEqualTo(1);
		assertThat(problem.getRange().getStart().getCharacter()).isEqualTo(0);
		assertThat(problem.getRange().getEnd().getLine()).isEqualTo(1);
		assertThat(problem.getRange().getEnd().getCharacter()).isEqualTo(3);

		document = new TextDocument("fakeuri", LanguageId.TXT, 0, "jack\nxxx\njack\nddd\njack");
		problems = linter.lint(document).toStream().collect(Collectors.toList());
		assertThat(problems).hasSize(2);
		problem = problems.get(0);
		assertThat(problem.getRange().getStart().getLine()).isEqualTo(1);
		assertThat(problem.getRange().getStart().getCharacter()).isEqualTo(0);
		assertThat(problem.getRange().getEnd().getLine()).isEqualTo(1);
		assertThat(problem.getRange().getEnd().getCharacter()).isEqualTo(3);
		problem = problems.get(1);
		assertThat(problem.getRange().getStart().getLine()).isEqualTo(3);
		assertThat(problem.getRange().getStart().getCharacter()).isEqualTo(0);
		assertThat(problem.getRange().getEnd().getLine()).isEqualTo(3);
		assertThat(problem.getRange().getEnd().getCharacter()).isEqualTo(3);
	}
}

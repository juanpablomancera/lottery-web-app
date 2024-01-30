
package lottery.pool;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.docs.Documenter.CanvasOptions;
import org.springframework.modulith.docs.Documenter.DiagramOptions;

import lottery.Application;

/**
 * Test case to verify the modular structure of the application.
 *
 * @author Oliver Drotbohm
 */
class ModularityTests {

	ApplicationModules modules = ApplicationModules.of(Application.class);

	@Test
	@Disabled
	void verifyModularity() {
		modules.verify();
	}

	@Test
	@Disabled
	void generateDocs() throws IOException {

		new Documenter(modules)
				.writeDocumentation(DiagramOptions.defaults(), CanvasOptions.defaults());
	}
}

package org.nrg.xdat.om.base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcProjectI;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.base.auto.AutoArcProject;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Creating a project no longer discards the cached archive specification, so the cache it keeps has to follow the
 * database the way a fresh load would. Each arc project is refreshed by its last-modified time; a change that doesn't
 * move that time is only seen once the project is evicted, which is what saving its arc settings does.
 */
public class BaseArcArchivespecificationTest {
    /** Each arc project in the "database", by ID, with its last-modified time. */
    private final Map<String, Long>   modified = new HashMap<>();
    /** What each arc project holds: stands for its fields and its child rows alike. */
    private final Map<String, String> contents = new HashMap<>();

    private MockedStatic<XDAT>           mockedXdat;
    // getArcProjectsById is declared on the generated parent, and a static mock only intercepts its declaring class.
    private MockedStatic<AutoArcProject> mockedArcProjects;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        final NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
        when(template.query(anyString(), any(ResultSetExtractor.class))).thenAnswer(invocation -> new HashMap<>(modified));
        mockedXdat = Mockito.mockStatic(XDAT.class);
        mockedXdat.when(XDAT::getNamedParameterJdbcTemplate).thenReturn(template);
        mockedArcProjects = Mockito.mockStatic(AutoArcProject.class);
        mockedArcProjects.when(() -> AutoArcProject.getArcProjectsById(any(), any(), anyBoolean())).thenAnswer(invocation -> {
            final String id = invocation.getArgument(0);
            return modified.containsKey(id) ? arcProject(id, modified.get(id), contents.get(id)) : null;
        });
        save("P1", 1000L, "prearchive_code=4");
        save("P2", 1000L, "prearchive_code=4");
    }

    @After
    public void tearDown() {
        mockedArcProjects.closeOnDemand();
        mockedXdat.closeOnDemand();
    }

    @Test
    public void aKeptSpecificationFollowsCreatesEditsAndDeletesLikeAFreshOne() {
        final ArcArchivespecification kept = specification();
        assertSameAsFresh(kept);

        save("P3", 2000L, "prearchive_code=4");   // created
        assertSameAsFresh(kept);

        save("P1", 3000L, "prearchive_code=0");   // edited, which moves its time
        assertSameAsFresh(kept);

        modified.remove("P2");                   // deleted
        contents.remove("P2");
        assertSameAsFresh(kept);
    }

    @Test
    public void aCreatedProjectIsLoadedOnItsOwn() {
        final ArcArchivespecification kept = specification();
        kept.getProjects_project();
        save("P3", 2000L, "prearchive_code=4");

        assertThat(kept.getProjectArc("P3")).isNotNull();

        // One load each for the first fill, then one for the new project: not the whole list again.
        mockedArcProjects.verify(() -> AutoArcProject.getArcProjectsById(eq("P1"), any(), anyBoolean()), times(1));
        mockedArcProjects.verify(() -> AutoArcProject.getArcProjectsById(eq("P2"), any(), anyBoolean()), times(1));
        mockedArcProjects.verify(() -> AutoArcProject.getArcProjectsById(eq("P3"), any(), anyBoolean()), times(1));
    }

    @Test
    public void aProjectRecreatedWithTheSameIdIsLoadedAgain() {
        final ArcArchivespecification kept = specification();
        assertThat(contentOf(kept, "P2")).isEqualTo("prearchive_code=4");

        // Deleted and created again with nothing looking it up in between: a new row, so a new time.
        modified.remove("P2");
        save("P2", 5000L, "prearchive_code=0");

        assertThat(contentOf(kept, "P2")).isEqualTo("prearchive_code=0");
    }

    @Test
    public void aChangeThatDoesNotMoveTheTimeIsSeenOnceTheProjectIsEvicted() {
        final ArcArchivespecification kept = specification();
        assertThat(contentOf(kept, "P1")).isEqualTo("prearchive_code=4");

        // A child row changed on its own, such as the project's paths: the arc project's time stays put.
        contents.put("P1", "prearchive_code=4, archivePath=/elsewhere");
        assertThat(contentOf(kept, "P1")).as("the refresh can't see it").isEqualTo("prearchive_code=4");

        kept.evictArcProject("P1");
        assertThat(contentOf(kept, "P1")).isEqualTo("prearchive_code=4, archivePath=/elsewhere");
        assertSameAsFresh(kept);
    }

    private void save(final String id, final long time, final String content) {
        modified.put(id, time);
        contents.put(id, content);
    }

    private void assertSameAsFresh(final ArcArchivespecification kept) {
        final ArcArchivespecification fresh = specification();
        assertThat(projects(kept)).isEqualTo(projects(fresh)).isEqualTo(contents);
    }

    private static String contentOf(final ArcArchivespecification specification, final String id) {
        return specification.getProjectArc(id).getCurrentArc();
    }

    /** The projects a specification lists, with what each holds. */
    private static Map<String, String> projects(final ArcArchivespecification specification) {
        final List<ArcProjectI> projects = specification.getProjects_project();
        return projects.stream().collect(Collectors.toMap(ArcProjectI::getId, ArcProjectI::getCurrentArc));
    }

    private static ArcArchivespecification specification() {
        return new ArcArchivespecification(mock(ItemI.class));
    }

    /** An arc project as loaded at one moment: its content frozen, as a loaded XFT item's is. */
    private static ArcProject arcProject(final String id, final long lastModified, final String content) {
        final XFTItem item = mock(XFTItem.class);
        when(item.getLastModified()).thenReturn(new Date(lastModified));
        final ArcProject project = mock(ArcProject.class);
        when(project.getId()).thenReturn(id);
        when(project.getItem()).thenReturn(item);
        when(project.getCurrentArc()).thenReturn(content);
        return project;
    }
}

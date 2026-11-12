package team.projectpulse.team;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * The team search compares the caller's text with MySQL's {@code lower()} of the stored name. MySQL lowercases
 * without a locale, so the Java side must too: under a Turkish default locale, {@code "I".toLowerCase()} is the
 * dotless {@code "ı"}, and a search for "IT Team" would match nothing.
 */
@SuppressWarnings("unchecked")
class TeamSpecsTest {

    private Locale savedDefault;

    private Root<Team> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder criteriaBuilder;
    private Expression<String> lowered;

    @BeforeEach
    void setUp() {
        this.savedDefault = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));

        this.root = mock(Root.class);
        this.query = mock(CriteriaQuery.class);
        this.criteriaBuilder = mock(CriteriaBuilder.class);
        this.lowered = mock(Expression.class);
        given(this.criteriaBuilder.lower(any())).willReturn(this.lowered);
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(this.savedDefault);
    }

    @Test
    void containsTeamNameLowercasesTheSearchTextWithoutTheServerLocale() {
        Path<Object> teamName = mock(Path.class);
        given(this.root.get("teamName")).willReturn(teamName);

        TeamSpecs.containsTeamName("IT Team").toPredicate(this.root, this.query, this.criteriaBuilder);

        verify(this.criteriaBuilder).like(this.lowered, "%it team%");
    }

    @Test
    void hasSectionNameLowercasesTheSectionNameWithoutTheServerLocale() {
        Path<Object> section = mock(Path.class);
        Path<Object> sectionName = mock(Path.class);
        given(this.root.get("section")).willReturn(section);
        given(section.get("sectionName")).willReturn(sectionName);

        TeamSpecs.hasSectionName("Section I").toPredicate(this.root, this.query, this.criteriaBuilder);

        verify(this.criteriaBuilder).equal(this.lowered, "section i");
    }
}

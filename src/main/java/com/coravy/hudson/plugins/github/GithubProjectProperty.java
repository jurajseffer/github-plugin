package com.coravy.hudson.plugins.github;

import com.cloudbees.jenkins.GitHubPushTrigger;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Stores the github related project properties.
 * <p>
 * As of now this is only the URL to the github project.
 *
 * @author Stefan Saasen <stefan@coravy.com>
 * @todo Should we store the GithubUrl instead of the String?
 */
public final class GithubProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    /**
     * This will the URL to the project main branch.
     */
    private String projectUrl;

    /**
     * Path within a monolithic repository, can be empty
     */
    private String repositoryPath;

    /**
     * Whether or not plugin should poll using Git SCM after an incoming push
     */
    private Boolean enablePolling;

    /**
     * Won't trigger a build if incoming Github push is for master branch
     */
    private Boolean ignoreMasterPush;

    /**
     * Will trigger a build only if incoming Github push is for master branch
     */
    private Boolean onlyMasterPush;

    @DataBoundConstructor
    public GithubProjectProperty(String projectUrl, String repositoryPath,
        Boolean enablePolling, Boolean ignoreMasterPush, Boolean onlyMasterPush) {
        this.projectUrl = new GithubUrl(projectUrl).baseUrl();
        this.repositoryPath = repositoryPath;
        this.enablePolling = enablePolling;
        this.ignoreMasterPush = ignoreMasterPush;
        this.onlyMasterPush = onlyMasterPush;
    }

    /**
     * @return the projectUrl
     */
    public GithubUrl getProjectUrl() {
        return new GithubUrl(projectUrl);
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public Boolean getEnablePolling() {
        return enablePolling;
    }

    public Boolean getIgnoreMasterPush() {
        return ignoreMasterPush;
    }

    public Boolean getOnlyMasterPush() {
        return onlyMasterPush;
    }

    @Override
    public Collection<? extends Action> getJobActions(AbstractProject<?, ?> job) {
        if (null != projectUrl) {
            return Collections.singleton(new GithubLinkAction(this));
        }
        return Collections.emptyList();
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        public DescriptorImpl() {
            super(GithubProjectProperty.class);
            load();
        }

        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        public String getDisplayName() {
            return "GitHub project page";
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            GithubProjectProperty tpp = req.bindJSON(GithubProjectProperty.class, formData);

            if (tpp == null) {
                LOGGER.fine("Couldn't bind JSON");
                return null;
            }
            if (tpp.projectUrl == null) {
                tpp = null; // not configured
                LOGGER.fine("projectUrl not found, nullifying GithubProjectProperty");
            }
            return tpp;
        }

    }

    private static final Logger LOGGER = Logger.getLogger(GitHubPushTrigger.class.getName());
}

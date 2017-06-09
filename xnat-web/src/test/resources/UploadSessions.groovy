import org.nrg.xnat.interfaces.XnatInterface
import org.nrg.xnat.pogo.Project
import org.nrg.xnat.pogo.Subject
import org.nrg.xnat.pogo.experiments.ImagingSession
import org.nrg.xnat.pogo.experiments.sessions.MRSession
import org.nrg.xnat.pogo.extensions.subject_assessor.SessionImportExtension

@GrabResolver(name='NRG Release Repo', root='https://nrgxnat.jfrog.io/nrgxnat/libs-release')
@GrabResolver(name='NRG Snapshot Repo', root='https://nrgxnat.jfrog.io/nrgxnat/libs-snapshot')
@Grapes([
        @GrabExclude("org.codehaus.groovy:groovy-xml"),
        @GrabExclude("org.codehaus.groovy:groovy-json"),
        @Grab(group = 'org.nrg', module = 'grxnat', version = '1.8.1')
])

XnatInterface xnatInterface = XnatInterface.authenticate('http://10.1.1.17', 'admin', 'admin')
Project project = new Project() // no ID provided, so a random ID will be assigned
1000.times {
    Subject subject = new Subject(project)
    ImagingSession session = new MRSession(project, subject)
    session.extension(new SessionImportExtension(xnatInterface, session, new File('1_file.zip')))
}
xnatInterface.createProject(project)
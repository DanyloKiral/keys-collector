package dto

case class GitHubApiSearchResponse(total_count: Int, incomplete_results: Boolean, items: List[GitHubApiSearchItem])

case class GitHubApiSearchItem(name: String, path: String, html_url: String, sha: String, url: String, repository: GitHubApiRepoShortDetails)

case class GitHubApiRepoShortDetails(id: Int, node_id: String, full_name: String, url: String, html_url: String, languages_url: String)

case class GitHubApiFile(name: String, path: String, sha: String, url: String, content: String, encoding: String)
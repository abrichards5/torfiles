import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';

import { SearchService } from '../../services';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';
import { Subscription } from 'rxjs/Subscription';

import { Tools, SearchResults } from '../../shared';

@Component({
	selector: 'app-search',
	templateUrl: './search.component.html',
	styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {

	public searchTerm: string = '';

	public rows: Array<any> = [];

	public sorting: any = {
		'path': '',
		'size_bytes': '',
		'age': '',
		'peers': ''
	};

	public searchSub: Subscription;

	public page: number = 1;
	public limit: number = 25;
	public maxPaginators: number = 5;
	public length: number = 1;
	public numPages: number;
	public loading: boolean = false;

	public torrentCount: number;
	public fileCount: number;

	constructor(private route: ActivatedRoute,
		private router: Router,
		private sanitizer: DomSanitizer,
		private searchService: SearchService) {

		this.getTableCounts();

	}

	ngOnInit() {

		this.route.params.subscribe(params => {

			this.setSearchParams(params);
			this.page = 1;
			this.onChangeTable();
		});

	}

	getTableCounts() {
		this.searchService.getTableCounts().subscribe(d => {
			this.torrentCount = d[1].count_;
			this.fileCount = d[0].count_;
		});
	}

	public setSearchParams(params: any) {
		this.searchTerm = (params['searchTerm']) ? params['searchTerm'] : '';
	}


	public onChangeTable(page: any = { page: this.page, limit: this.limit }): any {
		this.loading = true;
		this.rows = null;
		this.page = page.page;

		// Stops the last search
		if (this.searchSub) {
			this.searchSub.unsubscribe();
		}

		this.searchSub = this.searchService.getSearchResults(this.searchTerm, this.limit, this.page).subscribe(d => {
			this.rows = d.results;
			this.length = d.count;
			this.loading = false;
		});
	}

	public generateMagnetLink(name, infoHash, index) {
		return Tools.generateMagnetLink(name, infoHash, index);
	}

	public getFileName(path: string): string {
		return Tools.getFileName(path);
	}
	
	public getTorrentName(path: string): string {
		return Tools.getTorrentName(path);
	}
}

